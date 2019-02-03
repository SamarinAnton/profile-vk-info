package ru.webim.testTask.profileInfo.service;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.friends.responses.GetFieldsResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.users.UserField;
import org.springframework.stereotype.Service;
import ru.webim.testTask.profileInfo.DTO.UserDTO;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class VkApiService {

    private static final int NUMBER_FRIENDS = 5;

    public UserActor initActor(VkApiClient vk, Integer appId, String secretKey, String redirectUrl, String code) throws ClientException, ApiException {
        UserAuthResponse authResponse = vk.oauth()
                .userAuthorizationCodeFlow(appId, secretKey, redirectUrl, code)
                .execute();

        return new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
    }

    public UserDTO getProfileInfo(VkApiClient vk, UserActor actor) throws ClientException, ApiException {
        List<UserXtrCounters> users = vk
                .users()
                .get(actor)
                .fields(UserField.NICKNAME, UserField.PHOTO_100)
                .execute();

        return new UserDTO(users.get(0).getFirstName(), users.get(0).getLastName(), users.get(0).getPhoto100());
    }

    public List<UserDTO> getFriends(VkApiClient vk, UserActor actor) throws ClientException, ApiException {
        GetFieldsResponse fullInfo = vk
                .friends()
                .get(actor, UserField.NICKNAME, UserField.PHOTO_100)
                .count(NUMBER_FRIENDS)
                .execute();

        return fullInfo
                .getItems()
                .stream().map(user ->
                            new UserDTO(user.getFirstName(), user.getLastName(), user.getPhoto100()))
                .collect(Collectors.toList());
    }

}
