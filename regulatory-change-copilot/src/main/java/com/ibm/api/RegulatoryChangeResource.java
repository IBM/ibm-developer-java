package com.ibm.api;

import com.ibm.ai.RegulatoryChangeImpactCopilot;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/copilot")
public class RegulatoryChangeResource {

    @Inject
    RegulatoryChangeImpactCopilot copilot;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ChatResponse ask(@QueryParam("q") String question) {
        if (question == null || question.trim().isEmpty()) {
            return new ChatResponse("Please provide a question about regulatory changes.");
        }

        String answer = copilot.chat(question);
        return new ChatResponse(answer);
    }

    public static record ChatResponse(String answer) {
    }
}