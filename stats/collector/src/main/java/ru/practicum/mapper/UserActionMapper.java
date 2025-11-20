package ru.practicum.mapper;

import com.google.protobuf.Timestamp;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.user.action.ActionTypeProto;
import ru.practicum.grpc.user.action.UserActionProto;

import java.time.Instant;

public class UserActionMapper {

    // USER ACTION OBJECTS

    public static UserActionProto fromAvroToProto(UserActionAvro avro) {
        return UserActionProto.newBuilder()
                .setUserId(avro.getUserId())
                .setEventId(avro.getEventId())
                .setActionType(fromAvroToProto(avro.getActionType()))
                .setTimestamp(fromAvroToProto(avro.getTimestamp()))
                .build();
    }

    public static UserActionAvro fromProtoToAvro(UserActionProto proto) {
        return UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(fromProtoToAvro(proto.getActionType()))
                .setTimestamp(fromProtoToAvro(proto.getTimestamp()))
                .build();
    }

    // ENUMS

    public static ActionTypeProto fromAvroToProto(ActionTypeAvro avro) {
        return switch (avro) {
            case ActionTypeAvro.LIKE -> ActionTypeProto.ACTION_LIKE;
            case ActionTypeAvro.REGISTER -> ActionTypeProto.ACTION_REGISTER;
            default -> ActionTypeProto.ACTION_VIEW;
        };
    }

    public static ActionTypeAvro fromProtoToAvro(ActionTypeProto proto) {
        return switch (proto) {
            case ActionTypeProto.ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ActionTypeProto.ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            default -> ActionTypeAvro.VIEW;
        };
    }

    // TIMESTAMP

    public static Timestamp fromAvroToProto(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static Instant fromProtoToAvro(Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

}
