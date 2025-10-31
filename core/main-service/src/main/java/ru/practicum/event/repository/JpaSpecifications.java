package ru.practicum.event.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.event.dto.EventAdminParams;
import ru.practicum.event.dto.EventParams;
import ru.practicum.event.model.Event;
import ru.practicum.request.ParticipationRequestStatus;
import ru.practicum.request.Request;

import java.util.ArrayList;
import java.util.List;

public class JpaSpecifications {

    public static Specification<Event> adminFilters(EventAdminParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.getUsers() != null && !params.getUsers().isEmpty())
                predicates.add(root.get("initiator").get("id").in(params.getUsers()));

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

            if (params.getOnlyAvailable() == true) {
                Join<Event, Request> requestJoin = root.join("requests", JoinType.LEFT);
                requestJoin.on(cb.equal(requestJoin.get("status"), ParticipationRequestStatus.CONFIRMED));
                query.groupBy(root.get("id"));

                Predicate unlimitedPredicate = cb.equal(root.get("participantLimit"), 0);
                Predicate hasFreeSeatsPredicate = cb.greaterThan(root.get("participantLimit"), cb.count(requestJoin));
                query.having(cb.or(unlimitedPredicate, hasFreeSeatsPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
