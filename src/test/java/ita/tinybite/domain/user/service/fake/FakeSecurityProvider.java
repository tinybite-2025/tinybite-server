package ita.tinybite.domain.user.service.fake;

import ita.tinybite.domain.auth.service.SecurityProvider;
import ita.tinybite.domain.user.entity.User;
import ita.tinybite.domain.user.repository.UserRepository;

public class FakeSecurityProvider extends SecurityProvider {

    private User currentUser;

    public FakeSecurityProvider(UserRepository userRepository) {
        super(userRepository);
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }
}
