package andrey.dev.userservice.mapper;

import andrey.dev.userservice.entity.PaymentCard;
import andrey.dev.userservice.entity.dto.PaymentCardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardResponseMapper {

    @Mapping(target = "userId", expression = "java(paymentCard.getUser().getId())")
    @Mapping(target = "number", expression = "java(maskCardNumber(paymentCard.getNumber()))")
    PaymentCardResponse toPaymentCardResponse(PaymentCard paymentCard);

    default String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) {
            return null;
        }

        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");

        if (cleanNumber.length() < 4) {
            return "****";
        }

        String last4 = cleanNumber.substring(cleanNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}
