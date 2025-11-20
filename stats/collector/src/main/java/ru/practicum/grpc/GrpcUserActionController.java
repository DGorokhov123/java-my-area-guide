package ru.practicum.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.collector.UserActionControllerGrpc;
import ru.practicum.grpc.user.action.UserActionProto;
import ru.practicum.service.UserActionService;

@GrpcService
@RequiredArgsConstructor
public class GrpcUserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionService userActionService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            userActionService.handleUserAction(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()).withCause(e))
            );
        }
    }

}
