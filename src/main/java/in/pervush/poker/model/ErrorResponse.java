package in.pervush.poker.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(@Schema(required = true) ErrorStatus status) {
}
