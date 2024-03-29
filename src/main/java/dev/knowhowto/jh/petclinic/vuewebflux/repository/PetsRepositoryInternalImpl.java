package dev.knowhowto.jh.petclinic.vuewebflux.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import dev.knowhowto.jh.petclinic.vuewebflux.domain.Pets;
import dev.knowhowto.jh.petclinic.vuewebflux.repository.rowmapper.OwnersRowMapper;
import dev.knowhowto.jh.petclinic.vuewebflux.repository.rowmapper.PetsRowMapper;
import dev.knowhowto.jh.petclinic.vuewebflux.repository.rowmapper.TypesRowMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the Pets entity.
 */
@SuppressWarnings("unused")
class PetsRepositoryInternalImpl extends SimpleR2dbcRepository<Pets, Long> implements PetsRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final TypesRowMapper typesMapper;
    private final OwnersRowMapper ownersMapper;
    private final PetsRowMapper petsMapper;

    private static final Table entityTable = Table.aliased("pets", EntityManager.ENTITY_ALIAS);
    private static final Table typeTable = Table.aliased("types", "e_type");
    private static final Table ownerTable = Table.aliased("owners", "owner");

    public PetsRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        TypesRowMapper typesMapper,
        OwnersRowMapper ownersMapper,
        PetsRowMapper petsMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Pets.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.typesMapper = typesMapper;
        this.ownersMapper = ownersMapper;
        this.petsMapper = petsMapper;
    }

    @Override
    public Flux<Pets> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Pets> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = PetsSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(TypesSqlHelper.getColumns(typeTable, "type"));
        columns.addAll(OwnersSqlHelper.getColumns(ownerTable, "owner"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(typeTable)
            .on(Column.create("type_id", entityTable))
            .equals(Column.create("id", typeTable))
            .leftOuterJoin(ownerTable)
            .on(Column.create("owner_id", entityTable))
            .equals(Column.create("id", ownerTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Pets.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Pets> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Pets> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Pets process(Row row, RowMetadata metadata) {
        Pets entity = petsMapper.apply(row, "e");
        entity.setType(typesMapper.apply(row, "type"));
        entity.setOwner(ownersMapper.apply(row, "owner"));
        return entity;
    }

    @Override
    public <S extends Pets> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
