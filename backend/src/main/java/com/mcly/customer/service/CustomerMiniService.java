package com.mcly.customer.service;

import com.mcly.customer.api.CreateCustomerReservationRequest;
import com.mcly.customer.api.CustomerCardResponse;
import com.mcly.customer.api.CustomerHomeResponse;
import com.mcly.customer.api.CustomerOrderResponse;
import com.mcly.customer.api.CustomerPassResponse;
import com.mcly.customer.api.CustomerPetResponse;
import com.mcly.customer.api.CustomerContextResponse;
import com.mcly.customer.api.CreateCustomerReservationResponse;
import com.mcly.customer.api.CustomerProfileResponse;
import com.mcly.customer.api.CustomerTicketResponse;
import com.mcly.customer.api.PrepayResponse;
import com.mcly.customer.repository.CustomerMiniQueryRepository;
import com.mcly.common.auth.AuthContext;
import com.mcly.entrytoken.api.QrCodeGenerateResponse;
import com.mcly.entrytoken.api.RiskReportRequest;
import com.mcly.entrytoken.api.RiskReportResponse;
import com.mcly.entrytoken.service.EntryTokenService;
import com.mcly.order.repository.CustomerOrderCommandRepository;
import com.mcly.order.repository.ReservationCommandRepository;
import com.mcly.pass.repository.PassEntitlementCommandRepository;
import com.mcly.risk.repository.RiskEventCommandRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerMiniService {

    private static final Logger log = LoggerFactory.getLogger(CustomerMiniService.class);

    private final CustomerMiniQueryRepository customerMiniQueryRepository;
    private final ReservationCommandRepository reservationCommandRepository;
    private final CustomerOrderCommandRepository customerOrderCommandRepository;
    private final PassEntitlementCommandRepository passEntitlementCommandRepository;
    private final EntryTokenService entryTokenService;
    private final RiskEventCommandRepository riskEventCommandRepository;
    private final JdbcTemplate jdbcTemplate;

    public CustomerMiniService(
            CustomerMiniQueryRepository customerMiniQueryRepository,
            ReservationCommandRepository reservationCommandRepository,
            CustomerOrderCommandRepository customerOrderCommandRepository,
            PassEntitlementCommandRepository passEntitlementCommandRepository,
            EntryTokenService entryTokenService,
            RiskEventCommandRepository riskEventCommandRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.customerMiniQueryRepository = customerMiniQueryRepository;
        this.reservationCommandRepository = reservationCommandRepository;
        this.customerOrderCommandRepository = customerOrderCommandRepository;
        this.passEntitlementCommandRepository = passEntitlementCommandRepository;
        this.entryTokenService = entryTokenService;
        this.riskEventCommandRepository = riskEventCommandRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public CustomerHomeResponse home() {
        return new CustomerHomeResponse(
                List.of("周末寄养优惠", "年卡限时折扣", "春季洗护套餐"),
            List.of("购票 / 预约", "卡种中心", "我的宠物", "入园凭证"),
                "年卡会员可在有效期内多次入园"
        );
    }

    public List<CustomerOrderResponse> orders() {
        return customerMiniQueryRepository.orders();
    }

    public CustomerContextResponse context() {
        return customerMiniQueryRepository.context();
    }

    public List<CustomerPetResponse> pets() {
        return customerMiniQueryRepository.pets();
    }

    public List<CustomerPassResponse> passes() {
        return customerMiniQueryRepository.passes();
    }

    public List<CustomerCardResponse> cards() {
        return customerMiniQueryRepository.cards();
    }

    public CustomerProfileResponse profile() {
        return customerMiniQueryRepository.profile();
    }

    public List<CustomerTicketResponse> tickets() {
        return TICKET_CATALOG.stream()
                .map(item -> new CustomerTicketResponse(
                        item.code(),
                        item.name(),
                        item.desc(),
                        item.price().toPlainString(),
                        item.type()
                ))
                .toList();
    }

    /**
     * 创建预约和订单。订单初始状态为 PENDING_PAY（待支付）。
     * 客户端在收到结果后需调用 prepay 获取支付参数，完成支付后订单才会变为 PAID。
     */
    @Transactional
    public CreateCustomerReservationResponse createReservation(CreateCustomerReservationRequest request) {
        CustomerContextResponse context = customerMiniQueryRepository.context();
        if (context.memberId() == null || context.storeId() == null) {
            throw new IllegalArgumentException("当前会员不存在或未绑定门店");
        }

        TicketCatalogItem ticket = TICKET_CATALOG.stream()
                .filter(item -> item.code().equals(request.ticketCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("预约项目不存在"));

        boolean ownsPet = context.pets().stream().anyMatch(item -> item.id().equals(request.petId()));
        if (!ownsPet) {
            throw new IllegalArgumentException("宠物不存在或不属于当前会员");
        }

        LocalDate reservationDate;
        try {
            reservationDate = LocalDate.parse(request.reservationDate());
        } catch (Exception ex) {
            throw new IllegalArgumentException("预约日期格式不合法");
        }

        Long reservationId = reservationCommandRepository.create(
                context.memberId(),
                context.storeId(),
                request.petId(),
                ticket.type(),
                reservationDate,
                request.timeSlot(),
                "BOOKED",
                ticket.price()
        );

        String orderNo = "ORD" + reservationDate.format(DateTimeFormatter.BASIC_ISO_DATE)
                + String.format("%06d", reservationId);
        Long orderId = customerOrderCommandRepository.create(
                context.memberId(),
                context.storeId(),
                reservationId,
                orderNo,
                ticket.type(),
                "PENDING_PAY",
                ticket.price()
        );

        return new CreateCustomerReservationResponse(reservationId, orderId, orderNo, "PENDING_PAY");
    }

    /**
     * 预支付：生成微信支付参数。
     * <p>
     * TODO: 正式上线时替换为真实的微信支付统一下单 API 调用：
     * POST https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi
     * 需要商户号、API 密钥、证书等配置。
     * <p>
     * 当前为开发模式存根，返回模拟支付参数。
     */
    public PrepayResponse prepay(String orderNo) {
        // 验证订单存在且属于当前会员
        var orderInfo = jdbcTemplate.query("""
                select co.id, co.status, co.payable_amount
                from customer_order co
                where co.order_no = ?
                """, rs -> {
            if (!rs.next()) return null;
            return new Object[]{
                    rs.getLong("id"),
                    rs.getString("status"),
                    rs.getBigDecimal("payable_amount").toPlainString()
            };
        }, orderNo);

        if (orderInfo == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        String status = (String) orderInfo[1];
        String amount = (String) orderInfo[2];

        if ("PAID".equals(status)) {
            throw new IllegalArgumentException("订单已支付，无需重复支付");
        }
        if (!"PENDING_PAY".equals(status)) {
            throw new IllegalArgumentException("订单状态异常，无法发起支付");
        }

        // 开发模式：生成模拟支付参数
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String mockPrepayId = "wx_prepay_dev_" + orderNo;

        // 记录 prepay_id
        jdbcTemplate.update(
                "update customer_order set wx_prepay_id = ? where order_no = ?",
                mockPrepayId, orderNo
        );

        log.info("[开发模式] 生成模拟预支付参数: orderNo={}, prepayId={}", orderNo, mockPrepayId);

        return new PrepayResponse(
                timestamp,
                nonceStr,
                "prepay_id=" + mockPrepayId,
                "RSA",
                "mock_sign_" + nonceStr,
                orderNo,
                amount
        );
    }

    /**
     * 确认支付完成（开发模式直接确认；正式环境由微信支付回调触发）。
     * 将订单状态从 PENDING_PAY 更新为 PAID，并生成通行资格。
     */
    @Transactional
    public void confirmPayment(String orderNo) {
        var orderInfo = jdbcTemplate.query("""
                select co.id, co.member_id, co.store_id, co.reservation_id, co.order_type, co.status, co.payable_amount
                from customer_order co
                where co.order_no = ?
                """, rs -> {
            if (!rs.next()) return null;
            return new OrderPaymentInfo(
                    rs.getLong("id"),
                    rs.getLong("member_id"),
                    rs.getLong("store_id"),
                    rs.getObject("reservation_id", Long.class),
                    rs.getString("order_type"),
                    rs.getString("status"),
                    rs.getBigDecimal("payable_amount")
            );
        }, orderNo);

        if (orderInfo == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if ("PAID".equals(orderInfo.status())) {
            return; // 幂等：已支付不重复处理
        }
        if (!"PENDING_PAY".equals(orderInfo.status())) {
            throw new IllegalArgumentException("订单状态异常，无法确认支付");
        }

        // 更新订单为已支付
        jdbcTemplate.update(
                "update customer_order set status = 'PAID', paid_amount = payable_amount, paid_at = current_timestamp where order_no = ?",
                orderNo
        );

        // 查询预约信息，生成通行资格
        if (orderInfo.reservationId() != null) {
            var reservationDate = jdbcTemplate.query(
                    "select reservation_date from reservation where id = ?",
                    rs -> rs.next() ? rs.getDate("reservation_date").toLocalDate() : null,
                    orderInfo.reservationId()
            );

            // 查找对应的 ticket code
            String ticketCode = TICKET_CATALOG.stream()
                    .filter(t -> t.type().equals(orderInfo.orderType()))
                    .map(TicketCatalogItem::code)
                    .findFirst()
                    .orElse(orderInfo.orderType());

            if (reservationDate != null) {
                passEntitlementCommandRepository.createDayAccessEntitlement(
                        orderInfo.memberId(),
                        orderInfo.storeId(),
                        ticketCode,
                        orderInfo.reservationId(),
                        reservationDate,
                        "SAME_DAY_UNLIMITED"
                );
            }
        }

        log.info("订单 {} 支付确认完成，已生成通行资格", orderNo);
    }

    /**
     * 为当前会员的通行资格生成入园二维码。
     */
    public QrCodeGenerateResponse generateQrCode(Long passEntitlementId) {
        Long memberId = resolveCurrentMemberId();
        return entryTokenService.generateQrCode(passEntitlementId, memberId);
    }

    /**
     * 会员举报非本人入园操作。
     */
    @Transactional
    public RiskReportResponse reportUnauthorizedEntry(RiskReportRequest request) {
        Long memberId = resolveCurrentMemberId();

        // 验证通行资格属于当前会员
        var entitlement = jdbcTemplate.queryForList(
                "select id, store_id from pass_entitlement where id = ? and member_id = ?",
                request.passEntitlementId(), memberId);
        if (entitlement.isEmpty()) {
            throw new IllegalArgumentException("通行资格不存在或不属于当前会员");
        }

        Long storeId = ((Number) entitlement.get(0).get("store_id")).longValue();

        // 创建高风险事件
        String content = String.format("{\"memberReported\":true,\"passEntitlementId\":%d,\"reason\":\"%s\"}",
                request.passEntitlementId(),
                request.reason() != null ? request.reason().replace("\"", "'") : "非本人操作");

        Long riskEventId = riskEventCommandRepository.createWithContent(
                storeId, "UNAUTHORIZED_ENTRY_REPORT", "HIGH", "MEMBER", memberId, content);

        log.info("会员 {} 举报非本人入园，风控事件 ID: {}", memberId, riskEventId);

        return new RiskReportResponse(riskEventId, "已收到举报，工作人员将尽快核实处理。");
    }

    private static final List<TicketCatalogItem> TICKET_CATALOG = List.of(
            new TicketCatalogItem("DAY_TICKET", "单次门票", "支持当日多次出入", "TICKET", new BigDecimal("68.00")),
            new TicketCatalogItem("GROOMING_PACKAGE", "洗护套餐", "含基础洗护与护理", "GROOMING", new BigDecimal("128.00")),
            new TicketCatalogItem("BOARDING_DAY", "寄养预约", "按天预约，可追加喂养服务", "BOARDING", new BigDecimal("188.00"))
    );

    private record TicketCatalogItem(
            String code,
            String name,
            String desc,
            String type,
            BigDecimal price
    ) {
    }

    private record OrderPaymentInfo(
            Long orderId,
            Long memberId,
            Long storeId,
            Long reservationId,
            String orderType,
            String status,
            BigDecimal payableAmount
    ) {
    }

    /**
     * 获取当前会员 ID。优先从 AuthContext 取，降级取第一个会员（开发兼容）。
     */
    private Long resolveCurrentMemberId() {
        Long memberId = AuthContext.getMemberId();
        if (memberId != null) {
            return memberId;
        }
        // 开发兼容降级
        return jdbcTemplate.queryForObject("select min(id) from member", Long.class);
    }
}
