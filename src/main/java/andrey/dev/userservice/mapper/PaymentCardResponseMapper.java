package andrey.dev.userservice.mapper;

import andrey.dev.userservice.entity.PaymentCard;
import andrey.dev.userservice.entity.dto.PaymentCardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardResponseMapper {

    @Mapping(target = "userId" , expression = "java(paymentCard.getUser().getId())")
    PaymentCardResponse toPaymentCardResponse(PaymentCard paymentCard);
}
