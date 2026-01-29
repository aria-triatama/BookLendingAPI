package com.aria.book.controller;

import com.aria.book.entity.Member;
import com.aria.book.repository.MemberRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberRepository memberRepository;

    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/list")
    public List<Member> listMembers() {
        return memberRepository.findAll();
    }

    @PostMapping("/create")
    public Member createMember(@RequestBody Member member) {
        return memberRepository.save(member);
    }

    @PutMapping("/update/{id}")
    public Member updateMember(@PathVariable UUID id, @RequestBody Member member) {
        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        existingMember.setName(member.getName());
        existingMember.setEmail(member.getEmail());
        return memberRepository.save(existingMember);
    }

    @GetMapping("/{id}")
    public Member getMemberById(@PathVariable UUID id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    @DeleteMapping("/delete/{id}")
    public void deleteMember(@PathVariable UUID id) {
        memberRepository.deleteById(id);
    }
}
