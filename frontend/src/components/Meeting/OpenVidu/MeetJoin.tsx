import axios from "axios";
import { OpenVidu } from "openvidu-browser";
import React, { useCallback, useEffect, useState } from "react";
import UserVideoComponent from "./UserVideoComponent";
import { MeetRoomData, MeetingProps, joinMeet } from "../../../api/meeting";

import { useRecoilState, useRecoilValue } from "recoil";
import { CurrentUserAtom } from "../../../recoil/Auth";
import { useNavigate } from "react-router-dom";
import {
  Container,
  // Chat,
  VideoContainer,
  // StreamContainerWrapper,
  StreamContainer,
  ChatBox,
  Session,
  ToolBar,
  Header,
  VedioInnerDiv,
  HeaderText,
} from "./MeetJoin.style";
import ChatComponent, { ChatProps } from "./ChatComponent";
import ToolbarComponent from "./ToolbarComponent";
import BadgeModal from "./BadgeModal";
import { meetDubUserState } from "/src/recoil/HW_Atom";
// import { VedioInnerDiv } from "./UserVideoComponent.style";

export interface streamContainerProps {
  curCount: number;
  bottomOn: boolean;
}
//https://i9b106.p.ssafy.io/openvidu/api/sessions/ses_GseS0kJaEF/connection"
const MeetJoin = ({ props }: { props: MeetingProps }) => {
  const navigate = useNavigate();
  const currentUser = useRecoilValue(CurrentUserAtom);
  const [mySessionId, setMySessionId] = useState(currentUser.userId);
  const [myUserName, setMyUserName] = useState(currentUser.nickname);
  const [userEmail, setUserEmail] = useState("");
  const [session, setSession] = useState<any>(undefined);
  const [publisher, setPublisher] = useState<any>(undefined);
  const [subscribers, setSubscribers] = useState<any[]>([]);
  const [meetDubUser, setMeetDubUser] = useRecoilState<number>(meetDubUserState);
  const [chatActive, setChatActive] = useState(true);

  const [connectionId, setConnectionId] = useState("");
  const [nickname, setNickname] = useState(currentUser.nickname);
  const [videoActive, setVideoActive] = useState(true);
  const [audioActive, setAudioActive] = useState(true);
  const [streamManagerTmp, setStreamManagerTmp] = useState<any>(undefined);
  const [curCount, setCurCount] = useState(0);

  const [roomData, setRoomData] = useState<any>();

  const [isOpenModal, setOpenModal] = useState<boolean>(false);

  const [time, setTime] = useState(0);
  const [hour, setHour] = useState(0);
  const [min, setMin] = useState(0);
  const [sec, setSec] = useState(0);

  useEffect(() => {
    (() => {
      window.addEventListener("beforeunload", onbeforeunload);
      window.addEventListener("popstate", popstateHandler);
    })();
    joinSession();

    return () => {
      window.removeEventListener("beforeunload", onbeforeunload);
      window.removeEventListener("popstate", popstateHandler);
    };
  }, []);

  useEffect(() => {
    const id = setInterval(() => {
      setTime(() => time + 1);
      setHour(Math.floor((time % 21600) / 3600));
      setMin(Math.floor((time % 3600) / 60));
      setSec(time % 60);
    }, 1000);
    return () => {
      clearInterval(id);
    };
  }, [time]);

  // useEffect(() => {
  //   if (messageReceived && chatDisplay === "none") {
  //     setMessageReceived(false);
  //   }
  // }, [messageReceived, chatDisplay]);

  useEffect(() => {
    setCurCount(subscribers.length + 1);
    setMeetDubUser(subscribers.length + 1);
  }, [subscribers]);

  const onbeforeunload = (event: BeforeUnloadEvent) => {
    event.preventDefault();
    alert("onbeforeunload");
    leaveSession();
  };

  const popstateHandler = () => {
    alert("popstateHandler");
    leaveSession();
  };

  const toggleChat = () => {
    setChatActive(!chatActive);
  };

  const deleteSubscriber = (streamManager: any) => {
    setSubscribers((prevSubscribers) => prevSubscribers.filter((sub) => sub !== streamManager));
  };

  const goMeetingBoard = () => {
    navigate("/meeting");
  };

  const joinSession = async () => {
    // --- 1) Get an OpenVidu object ---
    const OV = new OpenVidu();

    // --- 2) Init a session ---
    const mySession = OV.initSession();
    setSession(mySession);

    // --- 3) Specify the actions when events take place in the session ---
    mySession.on("streamCreated", (event) => {
      // event.stream.streamId = currentUser.email;
      const subscriber = mySession.subscribe(event.stream, "");
      setSubscribers((subscribers) => [...subscribers, subscriber]);
      setConnectionId(event.stream.connection.connectionId);
      console.log(subscriber);
    });

    mySession.on("streamDestroyed", (event) => {
      console.log("streamDestroyed");
      deleteSubscriber(event.stream.streamManager);
    });

    mySession.on("exception", (exception) => {
      console.warn(exception);
    });

    console.log(mySession);

    // --- 4) Connect to the session with a valid user token ---
    try {
      const token = await getToken();
      console.log(token);

      await mySession.connect(token, { clientData: currentUser.email });

      const devices = await OV.getDevices();
      console.log("devices");
      console.log(devices);
      const videoDevices = devices.filter((device) => device.kind === "videoinput");

      // --- 5) Get your own camera stream ---
      const newPublisher = OV.initPublisher("", {
        videoSource: videoDevices[1]?.deviceId,
        publishAudio: !audioActive,
        publishVideo: !videoActive,
        frameRate: 30,
        mirror: false,
        // insertMode: 'APPEND',
      });
      newPublisher.id = currentUser.email;
      await mySession.publish(newPublisher);
      setPublisher(newPublisher);
      console.log(newPublisher);
      // console.log(publisher);
    } catch (error: any) {
      console.log("There was an error connecting to the session:", error.code, error.message);
      leaveSession();
    }
  };

  const leaveSession = () => {
    // --- 7) Leave the session by calling 'disconnect' method over the Session object ---
    if (session) {
      session.disconnect();
    }

    // Empty all properties...
    setSession(undefined);
    setSubscribers([]);
    setMySessionId("SessionA");
    setMyUserName("Participant" + Math.floor(Math.random() * 100));
    setPublisher(undefined);
    goMeetingBoard();
  };

  // interface userChanged {}
  const sendSignalUserChanged = (data: any) => {
    const signalOptions = {
      data: JSON.stringify(data),
      type: "userChanged",
    };
    session.signal(signalOptions);
  };

  const camStatusChanged = () => {
    setVideoActive(!videoActive);
    console.log(videoActive);
    publisher.publishVideo(videoActive);
    // sendSignalUserChanged({ isVideoActive: videoActive });
  };

  const micStatusChanged = () => {
    setAudioActive(!audioActive);
    console.log(audioActive);
    publisher.publishAudio(audioActive);
    // sendSignalUserChanged({ isAudioActive: audioActive });
  };

  const nicknameChanged = (nickname: string) => {
    setNickname(nickname);
    sendSignalUserChanged({ nickname: nickname });
  };

  //-----------------------------------

  const getToken = async () => {
    const res = await joinMeet(props);
    if (res.message !== undefined) alert(res.message);
    else setRoomData(res);
    console.log(res);
    return res.token;
  };

  //   {
  //     "token": "wss://i9b106.p.ssafy.io?sessionId=bda526ac-d9a4-4d0b-9261-36559e48a7b5&token=tok_OldmH9XaXA5ZUpm2",
  //     "status": "입장",
  //     "meetRoomId": 107,
  //     "category": "DUB",
  //     "title": "111111",
  //     "maxCount": 2,
  //     "currentCount": 0,
  //     "createdAt": 1690876928365
  //   }

  const onClickToggleModal = useCallback(() => {
    setOpenModal(!isOpenModal);
  }, [isOpenModal]);

  const streamContainerProps: streamContainerProps = {
    curCount: curCount,
    bottomOn: props.bottomOn,
  };

  return (
    <Container>
      <Header id="session-header">
        {roomData !== undefined ? (
          <>
            <HeaderText>{roomData.title} </HeaderText>{" "}
            <span style={{ fontSize: "10px", color: "gray" }}>
              &#40;{curCount}/{roomData.maxCount}&#41;
            </span>
          </>
        ) : (
          <></>
        )}
        <HeaderText>
          {hour.toString().length < 2 ? "0" + hour : hour}:
          {min.toString().length < 2 ? "0" + min : min}:
          {sec.toString().length < 2 ? "0" + sec : sec}
        </HeaderText>
      </Header>
      {session !== undefined ? (
        <Session id="session" $chatActive={chatActive}>
          <VideoContainer>
            {publisher !== undefined ? (
              <StreamContainer $streamContainerProps={streamContainerProps}>
                {/* <>{JSON.parse(publisher.connection.data).clientData}</> */}
                {/* <>{publisher.connection}</> */}
                <UserVideoComponent streamManager={publisher} videoActive={videoActive} />
              </StreamContainer>
            ) : null}
            {subscribers.map((sub, i) => (
              <StreamContainer key={i} $streamContainerProps={streamContainerProps}>
                {/* <>{JSON.parse(publisher.connection.data).clientData}</> */}
                {/* <>{sub.connection}</> */}
                <UserVideoComponent
                  streamManager={sub}
                  onClickToggleModal={onClickToggleModal}
                  isOpenModal={isOpenModal}
                  videoActive={videoActive}
                />
              </StreamContainer>
            ))}
          </VideoContainer>
        </Session>
      ) : (
        <button onClick={joinSession}></button>
      )}
      {session !== undefined ? (
        <ChatBox $chatActive={chatActive}>
          {streamManagerTmp !== undefined ? (
            <ChatComponent
              chatProps={{
                connectionIdProps: connectionId,
                nicknameProps: nickname,
                streamManagerProps: streamManagerTmp,
                bottomOn: props.bottomOn,
              }}
            />
          ) : publisher !== undefined ? (
            <ChatComponent
              chatProps={{
                connectionIdProps: connectionId,
                nicknameProps: nickname,
                streamManagerProps: publisher,
                bottomOn: props.bottomOn,
              }}
            />
          ) : (
            <></>
          )}
        </ChatBox>
      ) : (
        <></>
      )}

      <ToolBar>
        <ToolbarComponent
          sessionId={mySessionId}
          audioActive={audioActive}
          videoActive={videoActive}
          chatActive={chatActive}
          // showNotification={messageReceived}
          camStatusChanged={camStatusChanged}
          micStatusChanged={micStatusChanged}
          toggleChat={toggleChat}
          // switchCamera={this.switchCamera}
          leaveSession={leaveSession}
        />
      </ToolBar>

      {isOpenModal && <BadgeModal onClickToggleModal={onClickToggleModal}>방 만들기</BadgeModal>}
    </Container>
  );
};

export default MeetJoin;