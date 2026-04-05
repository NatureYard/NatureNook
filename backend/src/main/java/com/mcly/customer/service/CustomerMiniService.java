package com.mcly.customer.service;

import com.mcly.customer.api.CreateCustomerReservationRequest;
import com.mcly.customer.api.CustomerCardResponse;
import com.mcly.customer.api.CustomerHomeResponse;
import com.mcly.customer.api.CustomerOrderResponse;
import com.mcly.customer.api.CustomerPetResponse;
import com.mcly.customer.api.CustomerReservationResponse;
import com.mcly.customer.repository.CustomerMiniQueryRepository;
import com.mcly.customer.repository.CustomerReservationCommandRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomerMiniService {

    private final CustomerMiniQueryRepository customerMiniQueryRepository;
    private final CustomerReservationCommandRepository customerReservationCommandRepository;

    public CustomerMiniService(
            CustomerMiniQueryRepository customerMiniQueryRepository,
            CustomerReservationCommandRepository customerReservationCommandRepository
    ) {
        this.customerMiniQueryRepository = customerMiniQueryRepository;
        this.customerReservationCommandRepository = customerReservationCommandRepository;
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

    public List<CustomerCardResponse> listCards() {
        return customerMiniQueryRepository.listCards();
    }

    public List<CustomerPetResponse> listPets() {
        return customerMiniQueryRepository.listPets();
    }

    public List<CustomerReservationResponse> listReservations() {
        return customerMiniQueryRepository.listReservations();
    }

    public Long createReservation(CreateCustomerReservationRequest request) {
        return customerReservationCommandRepository.create(request);
    }
}

