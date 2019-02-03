package ru.webim.testTask.profileInfo.controller;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.webim.testTask.profileInfo.DTO.UserDTO;
import ru.webim.testTask.profileInfo.service.VkApiService;

import java.util.List;

@Scope("session")
@Controller
@RequestMapping("/")
public class UserController {
    private VkApiClient vk;
    private UserActor actor;
    private VkApiService vkApiService;
    final private Integer APP_ID = 6841360;
    final private String CLIENT_SECRET = "1KJCMGPmDGWNubYu8iCu";
    final private String REDIRECT_URI = "http://localhost:8080/info";
    final private String SCOPE = "friends,offline ";
    final private String AUTHORIZE_URL = "https://oauth.vk.com/authorize";


    UserController() {
        TransportClient transportClient = HttpTransportClient.getInstance();
        this.vk = new VkApiClient(transportClient);
    }

    @Autowired
    public void setVkApiService(VkApiService vkApiService) {
        this.vkApiService = vkApiService;
    }

    @GetMapping
    public String startPageWithButton(Model data) {
        if (actor != null) {
            data.addAttribute("url", "info");
        } else {
            data.addAttribute("url", AUTHORIZE_URL);
            data.addAttribute("client_id", APP_ID);
            data.addAttribute("redirect_uri", REDIRECT_URI);
            data.addAttribute("scope", SCOPE);
        }
        return "main";
    }

    @GetMapping("/info")
    public String info(@RequestParam(value="code", defaultValue="") String code, Model data) {
        if (actor == null) {
            try {
                actor = vkApiService.initActor(vk, APP_ID, CLIENT_SECRET, REDIRECT_URI, code);
            } catch (ClientException | ApiException exception) {
                data.addAttribute("message", "Error while getting token");
                data.addAttribute("exceptionMessage", exception.getMessage());
                return "error";
            }
        }

        UserDTO user;
        List<UserDTO> friends;
        try {
            user = vkApiService.getProfileInfo(vk, actor);
            friends = vkApiService.getFriends(vk, actor);
        } catch (ClientException | ApiException exception) {
            data.addAttribute("message", "Error while getting data");
            data.addAttribute("exceptionMessage", exception.getMessage());
            return "error";
        }

        data.addAttribute("user", user);
        data.addAttribute("friends", friends);

        return "info";
    }
}