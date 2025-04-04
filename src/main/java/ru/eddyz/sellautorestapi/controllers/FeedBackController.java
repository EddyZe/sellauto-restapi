package ru.eddyz.sellautorestapi.controllers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.eddyz.sellautorestapi.dto.NewFeedBackDto;
import ru.eddyz.sellautorestapi.dto.UserFeedBackDto;
import ru.eddyz.sellautorestapi.entities.FeedBack;
import ru.eddyz.sellautorestapi.enums.Role;
import ru.eddyz.sellautorestapi.exeptions.FeedBackException;
import ru.eddyz.sellautorestapi.mapper.FeedBackMapper;
import ru.eddyz.sellautorestapi.service.FeedBackService;
import ru.eddyz.sellautorestapi.service.UserService;

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
    public ResponseEntity<?> sendFeedBack(@RequestBody NewFeedBackDto newFeedBackDto,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        var sender = userService.findByEmail(userDetails.getUsername());
        var receiver = userService.findById(newFeedBackDto.getReceiverId());
        var receiverFeedbacks = receiver.getReceivedFeedBacks();
        var newFeedback = FeedBack.builder()
                .text(newFeedBackDto.getText())
                .receiver(receiver)
                .sender(sender)
                .estimation(newFeedBackDto.getEstimation().doubleValue())
                .build();
        newFeedback = feedBackService.saveFeedBack(newFeedback);
        receiverFeedbacks.add(newFeedback);
        var newRating = receiverFeedbacks.stream()
                .mapToDouble(FeedBack::getEstimation)
                .average()
                .orElse(receiver.getRating());

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
            user.getReceivedFeedBacks().removeIf(f -> f.getId().equals(feedBackId));
            var newRating = user.getReceivedFeedBacks()
                    .stream()
                    .mapToDouble(FeedBack::getEstimation)
                    .average()
                    .orElse(user.getRating());
            user.setRating(newRating);
            return ResponseEntity.ok().build();
        }

        throw new FeedBackException("Invalid feedback id");
    }
}
