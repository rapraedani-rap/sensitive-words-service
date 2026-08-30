package za.co.flash.sensitivewords.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.co.flash.sensitivewords.dto.SanitizationResponse;
import za.co.flash.sensitivewords.service.SanitizationService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SanitizationController.class)
class SanitizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SanitizationService sanitizationService;


    @Test
    void shouldSanitizeText() throws Exception {

        SanitizationResponse response = SanitizationResponse.builder()
                .sanitizedText("************* the database")
                .build();

        when(sanitizationService.sanitize("select * from the database"))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/sanitize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "text": "select * from the database"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sanitizedText")
                        .value("************* the database"));

        verify(sanitizationService)
                .sanitize("select * from the database");
    }


    @Test
    void shouldReturnBadRequestWhenTextIsMissing() throws Exception {

        mockMvc.perform(post("/api/v1/sanitize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldReturnBadRequestWhenTextIsBlank() throws Exception {

        mockMvc.perform(post("/api/v1/sanitize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "text": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}