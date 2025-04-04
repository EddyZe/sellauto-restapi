package ru.eddyz.sellautorestapi.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.eddyz.sellautorestapi.entities.FeedBack;
import ru.eddyz.sellautorestapi.exeptions.FeedBackNotFoundException;
import ru.eddyz.sellautorestapi.repositories.FeedBackRepository;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class FeedBackService {
    private final FeedBackRepository feedBackRepository;

    @Transactional
    public List<FeedBack> findReceivedFeedbackByUserId(Long userId) {
        return feedBackRepository.findReceivedFeedbackByUserId(userId);
    }

    public FeedBack saveFeedBack(FeedBack feedBack) {
        return feedBackRepository.save(feedBack);
    }

    @Transactional
    public FeedBack findById(Long id) {
        return feedBackRepository.findById(id)
                .orElseThrow(() -> new FeedBackNotFoundException("FeedBack not found"));
    }

    public void deleteById(Long id) {
        if (feedBackRepository.findById(id).isEmpty())
            throw new FeedBackNotFoundException("FeedBack not found");

        feedBackRepository.deleteById(id);
    }
}
