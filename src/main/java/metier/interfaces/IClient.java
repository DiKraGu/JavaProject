package metier.interfaces;

import dao.Reparation;
import exception.NotFoundException;

public interface IClient {

    Reparation suivreReparationParCode(String codeSuivi) throws NotFoundException;

}
