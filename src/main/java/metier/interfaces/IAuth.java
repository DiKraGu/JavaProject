package metier.interfaces;

import dao.User;
import exception.AuthException;

public interface IAuth {

    User seConnecter(String email, String motDePasse) throws AuthException;

}
