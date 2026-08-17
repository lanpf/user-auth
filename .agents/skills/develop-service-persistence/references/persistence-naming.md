# Persistence Naming Rules

## Shared SQL XML

- **NAME-MYBATIS-SQL-001** — Name shared MyBatis/MyBatis-Plus fragment files `<Aggregate>SqlFragments.xml` with namespace `<service-package>.persistence.sql.<Aggregate>SqlFragments`; name concrete mapper files `<Aggregate>Mapper.xml` and reference shared `<sql>` elements with fully qualified `refid`.
