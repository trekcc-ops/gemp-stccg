package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = BasicValueSourceDeserializer.class)
public abstract class BasicValueSource implements ValueSource {
}