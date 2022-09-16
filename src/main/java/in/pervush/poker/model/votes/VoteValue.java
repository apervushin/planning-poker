package in.pervush.poker.model.votes;

import in.pervush.poker.model.tasks.Scale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum VoteValue {

    // Fibonacci
    VALUE_0(Scale.FIBONACCI),
    VALUE_1(Scale.FIBONACCI),
    VALUE_2(Scale.FIBONACCI),
    VALUE_3(Scale.FIBONACCI),
    VALUE_5(Scale.FIBONACCI),
    VALUE_8(Scale.FIBONACCI),
    VALUE_13(Scale.FIBONACCI),
    VALUE_21(Scale.FIBONACCI),

    // Clothes size
    SIZE_XS(Scale.CLOTHES_SIZE),
    SIZE_S(Scale.CLOTHES_SIZE),
    SIZE_M(Scale.CLOTHES_SIZE),
    SIZE_L(Scale.CLOTHES_SIZE),
    SIZE_XL(Scale.CLOTHES_SIZE);

    @Getter
    private final Scale scale;
}
