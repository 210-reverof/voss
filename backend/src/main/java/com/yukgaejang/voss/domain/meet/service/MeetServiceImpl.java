package com.yukgaejang.voss.domain.meet.service;

import com.yukgaejang.voss.domain.meet.exception.ExceedMaxNumber;
import com.yukgaejang.voss.domain.meet.exception.NoMeetRoomException;
import com.yukgaejang.voss.domain.meet.exception.WrongPinException;
import com.yukgaejang.voss.domain.meet.repository.MeetJoinRepository;
import com.yukgaejang.voss.domain.meet.repository.MeetRepository;
import com.yukgaejang.voss.domain.meet.repository.entity.Meet;
import com.yukgaejang.voss.domain.meet.repository.entity.MeetJoin;
import com.yukgaejang.voss.domain.meet.service.dto.request.CreateSessionIdRequest;
import com.yukgaejang.voss.domain.meet.service.dto.request.JoinMeetRoomRequest;
import com.yukgaejang.voss.domain.meet.service.dto.response.InitMeetRoomResponse;
import com.yukgaejang.voss.domain.meet.service.dto.response.JoinMeetRoomResponse;
import com.yukgaejang.voss.domain.meet.service.dto.response.ViewAllMeetRoomResponse;
import com.yukgaejang.voss.domain.member.exception.NoMemberException;
import com.yukgaejang.voss.domain.member.exception.WrongPasswordException;
import com.yukgaejang.voss.domain.member.repository.MemberRepository;
import com.yukgaejang.voss.domain.member.repository.entity.Member;
import com.yukgaejang.voss.infra.openvidu.OpenViduConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MeetServiceImpl implements MeetService{

    private final MeetRepository meetRepository;
    private final MemberRepository memberRepository;
    private final MeetJoinRepository meetJoinRepository;

    @Override
    public Page<ViewAllMeetRoomResponse> getMeetList(int page, int limit) {
        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<Meet> all = meetRepository.findAllList(pageRequest);
        return all.map(o -> new ViewAllMeetRoomResponse(o));
    }

    @Override
    public InitMeetRoomResponse initMeetRoom(CreateSessionIdRequest createSessionIdRequest) {
        // openvidu 세션 생성
        OpenViduConnection openViduConnection = new OpenViduConnection();
        String sessionId = openViduConnection.session();
        Optional<Member> findMember = memberRepository.findByEmail(createSessionIdRequest.getEmail());
        boolean isPassword = createSessionIdRequest.getPassword()==null?false:true;
        Meet meet = new Meet(createSessionIdRequest.getCategory(), createSessionIdRequest.getTitle(),
                createSessionIdRequest.getMaxCount(), isPassword, false, sessionId, createSessionIdRequest.getPassword());
        meetRepository.save(meet);
        Member member = findMember.orElseThrow(() -> new NoMemberException("회원이 아닙니다."));
        meetJoinRepository.save(new MeetJoin(member, meet));
        return new InitMeetRoomResponse(sessionId, meet.getId());
    }

    @Override
    public JoinMeetRoomResponse joinMeetRoom(JoinMeetRoomRequest joinMeetRoomRequest) {
        Optional<Meet> findMeet = meetRepository.findByMeetId(joinMeetRoomRequest.getMeetId());
        Meet meet = findMeet.orElseThrow(() -> new NoMeetRoomException("해당 방이 없습니다."));
        int maxCount = meet.getMaxCount();
        List<MeetJoin> joinMeetList = meetJoinRepository.findByMeetId(joinMeetRoomRequest.getMeetId());
        if (maxCount <= joinMeetList.size()) throw new ExceedMaxNumber("이미 방이 가득 찼습니다.");

        String password = meet.getPassword();
        if (!password.equals(joinMeetRoomRequest.getPassword())) throw new WrongPinException("비밀번호가 틀립니다");

        Optional<Member> findMember = memberRepository.findByEmail(joinMeetRoomRequest.getEmail());
        Member member = findMember.orElseThrow(() -> new NoMemberException("회원이 아닙니다."));
        meetJoinRepository.save(new MeetJoin(member, meet));
        return new JoinMeetRoomResponse(meet.getSessionId(), "입장");
    }
}
