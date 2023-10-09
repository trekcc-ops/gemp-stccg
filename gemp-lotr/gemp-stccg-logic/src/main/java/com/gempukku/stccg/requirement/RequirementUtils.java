package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.DefaultActionContext;

import java.util.Arrays;
import java.util.List;

public class RequirementUtils {

    public static boolean acceptsAllRequirements(Requirement[] requirementArray, DefaultActionContext actionContext) {
        return Arrays.stream(requirementArray).allMatch(requirement -> requirement.accepts(actionContext));
    }

    public static boolean acceptsAllRequirements(List<Requirement> requirementList, DefaultActionContext actionContext) {
        return requirementList.stream().allMatch(requirement -> requirement.accepts(actionContext));
    }

    public static boolean acceptsAnyRequirements(Requirement[] requirementArray, DefaultActionContext actionContext) {
        return Arrays.stream(requirementArray).anyMatch(requirement -> requirement.accepts(actionContext));
    }

}
