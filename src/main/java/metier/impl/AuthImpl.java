package metier.impl;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import dao.User;
import exception.AuthException;
import metier.impl.persistence.JpaUtil;
import metier.interfaces.IAuth;

public class AuthImpl implements IAuth {

    private EntityManager em;

    public AuthImpl() {
        this.em = JpaUtil.getEntityManager();
    }

    @Override
    public User seConnecter(String email, String motDePasse) throws AuthException {
        try {
            TypedQuery<User> q = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class);
            q.setParameter("email", email);
            User u = q.getSingleResult();

            if (!u.getMotDePasse().equals(motDePasse)) {
                throw new AuthException("Mot de passe incorrect");
            }
            return u;

        } catch (NoResultException e) {
            throw new AuthException("Utilisateur introuvable");
        }
    }
}
