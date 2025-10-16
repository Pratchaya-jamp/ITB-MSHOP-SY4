package intregrated.backend.utils;

import intregrated.backend.entities.accounts.UsersAccount;

public final class UserTypeResolver {
    private UserTypeResolver() {
        // Prevent instantiation
    }

    public static String resolveUserType(UsersAccount user) {
        boolean isBuyer = user.getBuyer() != null;
        boolean isSeller = user.getSeller() != null;

        if (isBuyer && isSeller) {
            return "USER, SELLER";
        } else if (isSeller) {
            return "SELLER";
        } else if (isBuyer) {
            return "BUYER";
        } else {
            return "USER";
        }
    }
}
