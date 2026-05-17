package andrey.dev.userservice.mapper;

import andrey.dev.userservice.entity.PaymentCard;
import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.PaymentCardResponse;
import andrey.dev.userservice.entity.dto.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = PaymentCardResponseMapper.class)
public interface UserResponseMapper {

    UserResponse toUserResponse(User user);

    List<PaymentCardResponse> toPaymentCardResponses(List<PaymentCard> paymentCards);
}
