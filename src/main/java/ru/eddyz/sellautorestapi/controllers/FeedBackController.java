package ru.eddyz.sellautorestapi.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.eddyz.sellautorestapi.dto.NewFeedBackDto;
import ru.eddyz.sellautorestapi.dto.UserFeedBackDto;
import ru.eddyz.sellautorestapi.entities.FeedBack;
import ru.eddyz.sellautorestapi.enums.Role;
import ru.eddyz.sellautorestapi.exeptions.FeedBackException;
import ru.eddyz.sellautorestapi.mapper.FeedBackMapper;
import ru.eddyz.sellautorestapi.service.FeedBackService;
import ru.eddyz.sellautorestapi.service.UserService;
import ru.eddyz.sellautorestapi.util.BindingResultHelper;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedBackController {
    private final FeedBackService feedBackService;
    private final UserService userService;
    private final FeedBackMapper feedBackMapper;

    @GetMapping("/{userId}")
    public ResponseEntity<?> userFeedBack(@PathVariable Long userId) {
        return ResponseEntity.ok(
                UserFeedBackDto.builder()
                        .feedbacks(feedBackService.findReceivedFeedbackByUserId(userId)
                                .stream()
                                .map(feedBackMapper::toDto)
                                .toList())
                        .build());
    }


    @PostMapping
    public ResponseEntity<?> sendFeedBack(@RequestBody @Valid NewFeedBackDto newFeedBackDto,
                                          BindingResult bindingResult,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        if (bindingResult.hasErrors()) {
            var resp = BindingResultHelper.buildFieldErrorMessage(bindingResult);
            return ResponseEntity.badRequest().body(
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, resp)
            );
        }

        var sender = userService.findByEmail(userDetails.getUsername());
        var receiver = userService.findById(newFeedBackDto.getReceiverId());

        if (sender.getUserId().equals(receiver.getUserId())) {
            throw new FeedBackException("You can't send a review to yourself");
        }

        var receiverFeedbacks = receiver.getReceivedFeedBacks();
        var newFeedback = FeedBack.builder()
                .text(newFeedBackDto.getText())
                .receiver(receiver)
                .sender(sender)
                .createdAt(LocalDateTime.now())
                .estimation(newFeedBackDto.getEstimation().doubleValue())
                .build();
        newFeedback = feedBackService.saveFeedBack(newFeedback);
        receiverFeedbacks.add(newFeedback);
        var newRating = receiverFeedbacks.stream()
                .mapToDouble(FeedBack::getEstimation)
                .average()
                .orElse(receiver.getRating() == null ? 0 : receiver.getRating());

        receiver.setRating(newRating);
        userService.update(receiver);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedBackMapper.toDto(newFeedback));

    }

    @DeleteMapping("/{feedBackId}")
    public ResponseEntity<?> deleteFeedBack(@PathVariable Long feedBackId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        var feedBack = feedBackService.findById(feedBackId);
        var user = userService.findByEmail(userDetails.getUsername());
        if (feedBack.getSender().getAccount().getEmail().equals(user.getAccount().getEmail()) ||
            user.getAccount().getRole() == Role.ROLE_ADMIN) {
            feedBackService.deleteById(feedBackId);
            feedBack.getReceiver().getReceivedFeedBacks().removeIf(f -> f.getId().equals(feedBackId));
            var newRating = feedBack.getReceiver().getReceivedFeedBacks()
                    .stream()
                    .mapToDouble(FeedBack::getEstimation)
                    .average()
                    .orElse(feedBack.getReceiver().getRating() ==  null ? 0 : feedBack.getReceiver().getRating());
            feedBack.getReceiver().setRating(newRating);
            userService.update(feedBack.getReceiver());
            return ResponseEntity.ok().build();
        }

        throw new FeedBackException("Invalid feedback id");
    }
}
