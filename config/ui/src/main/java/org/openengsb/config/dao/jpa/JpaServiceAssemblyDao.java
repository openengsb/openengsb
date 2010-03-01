package org.openengsb.config.dao.jpa;

import org.openengsb.config.dao.ServiceAssemblyDao;
import org.openengsb.config.domain.ServiceAssembly;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class JpaServiceAssemblyDao extends JpaBaseDao<ServiceAssembly> implements ServiceAssemblyDao {

}
