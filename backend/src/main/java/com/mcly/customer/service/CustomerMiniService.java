package com.mcly.customer.service;

import com.mcly.customer.api.CustomerHomeResponse;
import com.mcly.customer.api.CustomerOrderResponse;
import com.mcly.customer.repository.CustomerMiniQueryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomerMiniService {

    private final CustomerMiniQueryRepository customerMiniQueryRepository;

    public CustomerMiniService(CustomerMiniQueryRepository customerMiniQueryRepository) {
        this.customerMiniQueryRepository = customerMiniQueryRepository;
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
}
