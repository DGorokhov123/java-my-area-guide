package ru.practicum.event.dal;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.dto.event.EventAdminParams;
import ru.practicum.dto.event.EventParams;

import java.util.ArrayList;
import java.util.List;

public class JpaSpecifications {

    public static Specification<Event> adminFilters(EventAdminParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.getUsers() != null && !params.getUsers().isEmpty())
                predicates.add(root.get("initiatorId").in(params.getUsers()));

            if (params.getStates() != null && !params.getStates().isEmpty())
                predicates.add(root.get("state").in(params.getStates()));

            if (params.getCategories() != null && !params.getCategories().isEmpty())
                predicates.add(root.get("category").get("id").in(params.getCategories()));

            if (params.getRangeStart() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), params.getRangeStart()));

            if (params.getRangeEnd() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), params.getRangeEnd()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> publicFilters(EventParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.getText() != null && !params.getText().isEmpty()) {
                String searchPattern = "%" + params.getText().toLowerCase() + "%";
                Predicate annotationPredicate = cb.like(cb.lower(root.get("annotation")), searchPattern);
                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(annotationPredicate, descriptionPredicate));
            }

            if (params.getCategories() != null && !params.getCategories().isEmpty())
                predicates.add(root.get("category").get("id").in(params.getCategories()));

            if (params.getPaid() != null) predicates.add(cb.equal(root.get("paid"), params.getPaid()));

            if (params.getRangeStart() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), params.getRangeStart()));

            if (params.getRangeEnd() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), params.getRangeEnd()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
