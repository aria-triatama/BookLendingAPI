package com.aria.book.controller;

import com.aria.book.entity.Member;
import com.aria.book.repository.MemberRepository;
import com.aria.book.security.ApiKeyFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private ApiKeyFilter apiKeyFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Member sampleMember;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        sampleMember = Member.builder()
                .id(memberId)
                .name("Test Member")
                .email("test@example.com")
                .build();
    }

    @Test
    void listMembers_shouldReturnListOfMembers() throws Exception {
        when(memberRepository.findAll()).thenReturn(Arrays.asList(sampleMember));

        mockMvc.perform(get("/members/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Member"));

        verify(memberRepository, times(1)).findAll();
    }

    @Test
    void createMember_shouldReturnCreatedMember() throws Exception {
        when(memberRepository.save(any(Member.class))).thenReturn(sampleMember);

        mockMvc.perform(post("/members/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Member"));

        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void updateMember_shouldReturnUpdatedMember() throws Exception {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(sampleMember));
        when(memberRepository.save(any(Member.class))).thenReturn(sampleMember);

        mockMvc.perform(put("/members/update/{id}", memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(memberId.toString()));

        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void deleteMember_shouldReturnOk() throws Exception {
        doNothing().when(memberRepository).deleteById(memberId);

        mockMvc.perform(delete("/members/delete/{id}", memberId))
                .andExpect(status().isOk());

        verify(memberRepository, times(1)).deleteById(memberId);
    }

    @Test
    void updateMember_shouldFailWhenIdIsNull() throws Exception {
        Member memberWithoutId = Member.builder()
                .name("No ID Member")
                .email("test@example.com")
                .build();

        mockMvc.perform(put("/members/update/{id}", memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberWithoutId)))
                .andExpect(status().isNotFound());
    }
}