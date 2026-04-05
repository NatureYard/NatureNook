package com.mcly.customer.service;

import com.mcly.customer.api.CustomerHomeResponse;
import com.mcly.customer.api.CustomerOrderResponse;
import com.mcly.customer.api.CustomerPetResponse;
import com.mcly.customer.api.CustomerCardResponse;
import com.mcly.customer.api.CustomerContextResponse;
import com.mcly.customer.api.CreateCustomerReservationRequest;
import com.mcly.customer.api.CreateCustomerReservationResponse;
import com.mcly.customer.api.CustomerProfileResponse;
import com.mcly.customer.api.CustomerTicketResponse;
import com.mcly.customer.repository.CustomerMiniQueryRepository;
import com.mcly.order.repository.CustomerOrderCommandRepository;
import com.mcly.order.repository.ReservationCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CustomerMiniService {

    private final CustomerMiniQueryRepository customerMiniQueryRepository;
    private final ReservationCommandRepository reservationCommandRepository;
    private final CustomerOrderCommandRepository customerOrderCommandRepository;

    public CustomerMiniService(
            CustomerMiniQueryRepository customerMiniQueryRepository,
            ReservationCommandRepository reservationCommandRepository,
            CustomerOrderCommandRepository customerOrderCommandRepository
    ) {
        this.customerMiniQueryRepository = customerMiniQueryRepository;
        this.reservationCommandRepository = reservationCommandRepository;
        this.customerOrderCommandRepository = customerOrderCommandRepository;
    }

    public CustomerHomeResponse home() {
        return new CustomerHomeResponse(
                List.of("周末寄养优惠", "年卡限时折扣", "春季洗护套餐"),
                List.of("购票预约", "卡种中心", "我的宠物", "入园凭证"),
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
                "PAID",
                ticket.price()
        );

        return new CreateCustomerReservationResponse(reservationId, orderId, orderNo, "PAID");
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
}
