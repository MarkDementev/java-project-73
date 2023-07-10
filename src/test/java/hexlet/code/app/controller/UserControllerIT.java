package hexlet.code.app.controller;

import hexlet.code.app.config.SpringConfigForIT;
import hexlet.code.app.model.User;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.utils.TestUtils;

import static hexlet.code.app.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.app.config.security.SecurityConfig.LOGIN;
import static hexlet.code.app.controller.UserController.USER_CONTROLLER_PATH;
import static hexlet.code.app.controller.UserController.ID;
import static hexlet.code.app.utils.TestUtils.asJson;
import static hexlet.code.app.utils.TestUtils.fromJson;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
@ActiveProfiles(TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)
public class UserControllerIT {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestUtils utils;

    @AfterEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void createUserTest() throws Exception {
        final var response = utils.perform(
                    post(USER_CONTROLLER_PATH).content(asJson(utils.getUserDto())).contentType(APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();
        final List<User> allCreatedUsers = userRepository.findAll();
        final User userFromResponse = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(allCreatedUsers).hasSize(1);
        assertNotNull(userFromResponse.getId());
        assertEquals(userFromResponse.getEmail(), utils.getUserDto().getEmail());
        assertEquals(userFromResponse.getFirstName(), utils.getUserDto().getFirstName());
        assertEquals(userFromResponse.getLastName(), utils.getUserDto().getLastName());
        assertNotNull(userFromResponse.getCreatedAt());
    }

    @Test
    public void getUserTest() throws Exception {
        utils.createDefaultUser();

        final User expectedUser = userRepository.findAll().get(0);
        final var response = utils.perform(
                        get(USER_CONTROLLER_PATH + ID, expectedUser.getId()),
                        expectedUser.getEmail()
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse();
        final User userFromResponse = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertEquals(expectedUser.getId(), userFromResponse.getId());
        assertEquals(expectedUser.getEmail(), userFromResponse.getEmail());
        assertEquals(expectedUser.getFirstName(), userFromResponse.getFirstName());
        assertEquals(expectedUser.getLastName(), userFromResponse.getLastName());
        assertNotNull(userFromResponse.getCreatedAt());
    }

    @Test
    public void getUsersTest() throws Exception {
        utils.createDefaultUser();

        final var response = utils.perform(get(USER_CONTROLLER_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        final List<User> allUsers = fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(allUsers).hasSize(1);
    }

    @Test
    public void updateUserTest() throws Exception {
        utils.createDefaultUser();

        Long createdUserId = userRepository.findAll().get(0).getId();
        final var response = utils.perform(put(USER_CONTROLLER_PATH + ID, createdUserId)
                        .content(asJson(utils.getSecondUserDto())).contentType(APPLICATION_JSON),
                        utils.getUserDto().getEmail())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        final User userFromResponse = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        final List<User> allUsers = userRepository.findAll();

        assertThat(allUsers).hasSize(1);
        assertEquals(userFromResponse.getId(), createdUserId);
        assertEquals(userFromResponse.getEmail(), utils.getSecondUserDto().getEmail());
        assertEquals(userFromResponse.getFirstName(), utils.getSecondUserDto().getFirstName());
        assertEquals(userFromResponse.getLastName(), utils.getSecondUserDto().getLastName());
        assertNotNull(userFromResponse.getCreatedAt());
    }

    @Test
    public void deleteUserTest() throws Exception {
        utils.createDefaultUser();

        Long createdUserId = userRepository.findAll().get(0).getId();

        utils.perform(delete(USER_CONTROLLER_PATH + ID, createdUserId),
                        utils.getUserDto().getEmail())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<User> allUsers = userRepository.findAll();

        assertThat(allUsers).hasSize(0);
    }

    @Test
    public void loginTest() throws Exception {
        utils.createDefaultUser();
        utils.perform(
                post(LOGIN).content(asJson(utils.getLoginDto())).contentType(APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}
