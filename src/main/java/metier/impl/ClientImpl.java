package metier.impl;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import dao.Reparation;
import exception.NotFoundException;
import metier.impl.persistence.JpaUtil;
import metier.interfaces.IClient;

public class ClientImpl implements IClient {

    private EntityManager em;

    public ClientImpl() {
        this.em = JpaUtil.getEntityManager();
    }

    @Override
    public Reparation suivreReparationParCode(String codeSuivi) throws NotFoundException {
        try {
        	TypedQuery<Reparation> q = em.createQuery(
        		    "SELECT DISTINCT r FROM Reparation r " +
        		    "LEFT JOIN FETCH r.lignes lr " +
        		    "LEFT JOIN FETCH lr.appareil " +
        		    "WHERE r.codeSuivi = :code", Reparation.class);

            q.setParameter("code", codeSuivi);
            return q.getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("Aucune réparation trouvée pour ce code");
        }
    }
}
