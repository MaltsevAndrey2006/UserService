package andrey.dev.userservice.repository.specification;

import andrey.dev.userservice.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> filterByFirstName(String firstName) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + firstName.toLowerCase() + "%");
    }

    public static Specification<User> filterBySurName(String surName) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("surname")), "%" + surName.toLowerCase() + "%");
    }

}
