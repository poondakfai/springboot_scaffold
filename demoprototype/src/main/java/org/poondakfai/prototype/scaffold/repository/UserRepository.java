package org.poondakfai.prototype.scaffold.repository;


import org.poondakfai.prototype.scaffold.model.User;
import org.springframework.data.repository.CrudRepository;
import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;


/**
 * A DAO for the entity CanonicalDomainName is simply created by extending the CrudRepository
 * interface provided by Spring. The following methods are some of the ones
 * available from such interface: save, delete, deleteAll, findOne and findAll.
 * The magic is that such methods must not be implemented, and moreover it is
 * possible create new query methods working only by defining their signature!
 *
 */
@Transactional
@Repository
public interface UserRepository extends CrudRepository<User, String> {
  public User findByUsername(String username);
}


