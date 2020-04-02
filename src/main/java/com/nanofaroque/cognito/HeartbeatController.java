package com.nanofaroque.cognito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HeartbeatController {
    @GetMapping("/heartbeat")
    public String showGreeting() {
        String userPoolId = "user_pool_id";
        AuthenticationResultType authenticationResult = null;
        AWSCognitoIdentityProvider cognito = AWSCognitoIdentityProviderClientBuilder.defaultClient();
        List<UserPoolDescriptionType> userPools =
                cognito.listUserPools(new ListUserPoolsRequest().withMaxResults(20)).getUserPools();

        ListUserPoolClientsResult response =
                cognito.listUserPoolClients(
                        new ListUserPoolClientsRequest()
                                .withUserPoolId(userPoolId)
                                .withMaxResults(1)
                );
        UserPoolClientType userPool =
                cognito.describeUserPoolClient(
                        new DescribeUserPoolClientRequest()
                                .withUserPoolId(userPoolId)
                                .withClientId(
                                        response.getUserPoolClients().get(0).getClientId()
                                )
                ).getUserPoolClient();


        Map<String, String> authParams = new HashMap<>(2);
        authParams.put("USERNAME", "ofaroque");
        authParams.put("PASSWORD", "Qwerty123!");
        AdminInitiateAuthRequest authRequest =
                new AdminInitiateAuthRequest()
                        .withClientId(userPool.getClientId())
                        .withUserPoolId(userPool.getUserPoolId())
                        .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                        .withAuthParameters(authParams);
        AdminInitiateAuthResult result =
                cognito.adminInitiateAuth(authRequest);
        AuthenticationResultType auth = result.getAuthenticationResult();


        if (!result.getChallengeName().isEmpty()) {
//If the challenge is required new Password validates if it has the new password variable.
            if ("NEW_PASSWORD_REQUIRED".equals(result.getChallengeName())) {
                final Map<String, String> challengeResponses = new HashMap<>();
                challengeResponses.put("USERNAME", "ofaroque");
                challengeResponses.put("PASSWORD", "Qwerty123!");
                //add the new password to the params map
                challengeResponses.put("NEW_PASSWORD", "Qwerty123!");
                //add the new password to the params map

                //populate the challenge response
                final AdminRespondToAuthChallengeRequest request =
                        new AdminRespondToAuthChallengeRequest();
                request.withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                        .withChallengeResponses(challengeResponses)
                        .withClientId(response.getUserPoolClients().get(0).getClientId())
                        .withUserPoolId(userPoolId)
                        .withSession(result.getSession());

                AdminRespondToAuthChallengeResult resultChallenge =
                        cognito.adminRespondToAuthChallenge(request);
                authenticationResult = resultChallenge.getAuthenticationResult();
                System.out.println(authenticationResult.getIdToken());
            }
        }

        return "";
    }
}