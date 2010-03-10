package org.openengsb.config.dao.jpa;

import org.openengsb.config.dao.EndpointDao;
import org.openengsb.config.domain.Endpoint;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class JpaEndpointDao extends JpaBaseDao<Endpoint> implements EndpointDao {

}
