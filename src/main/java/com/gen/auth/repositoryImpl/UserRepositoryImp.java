package com.gen.auth.repositoryImpl;

import com.gen.auth.model.Permissions;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@AllArgsConstructor
public class UserRepositoryImp {

    private final  Jdbi jdbi;

    public String getValue(String query) {
        return jdbi.inTransaction(handle ->
                handle.createQuery(query)
                        .mapTo(String.class)
                        .one()
        );
    }

    public List<Permissions> getPermissions(Long userId) {

        return jdbi.inTransaction(handle -> {

            String query = """
                SELECT DISTINCT p.name
                FROM permissions p
                JOIN permission_set_permissions psp
                    ON psp.permission_id = p.id
                JOIN role_permission_sets rps
                    ON rps.permission_set_id = psp.permission_set_id
                JOIN user_roles ur on ur.role_id=rps.role_id and ur.user_id= :userId
                """;

            return handle.createQuery(query)
                    .bind("userId", userId)
                    .mapToBean(Permissions.class)
                    .list();
        });
    }

    public Set<String> getRolesByUserId(Long userId){
        return jdbi.inTransaction(handle -> handle.createQuery("select distinct name from roles r join user_roles ur on r.id=ur.role_id join users u on u.id=ur.user_id").mapTo(String.class).set());
    }
}
