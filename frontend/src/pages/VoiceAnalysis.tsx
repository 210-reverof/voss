import { styled } from 'styled-components';
import { BackGroundImg } from '../components/BackGroundImg';
import Header from '../components/Header/Header';
import Script from '../components/VoiceAnalysis/Script';
import AnalysisButton from '../components/VoiceAnalysis/AnalysisButton/AnalysisButton';
import Messenger from '../components/Message/Messenger';
import RecordButton from '../components/VoiceAnalysis/RecordButton/RecordButton';
import Recording from '../components/VoiceAnalysis/Recording/Recording';

const Container = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
`
const UpSection = styled.div`
  display: flex;
`
const LeftSection = styled.div`
  display: flex;
  flex-direction: column;
  margin-right: 30px;
`
const RightSection = styled.div`
  display: flex;
  flex-direction: column;
`

function VoiceAnalysis() {
  return (
    <BackGroundImg>
      <Header/>
      <Container>
        <UpSection>
          <LeftSection>
            <Script></Script>
          </LeftSection>
          <RightSection>
            <Recording></Recording>
            <AnalysisButton></AnalysisButton>
          </RightSection>
        </UpSection>
        <RecordButton></RecordButton>
      </Container>
      <Messenger/>
    </BackGroundImg>
  )
}
export default VoiceAnalysis