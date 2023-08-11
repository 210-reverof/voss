import { styled } from 'styled-components';

export const ResultBox = styled.div`
  background-color: #2E2E2E;
  border-radius: 8px;
  width: 470px;
  height: 200px;
`

export const Section = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 25px;
`

export const Text = styled.div`
  font-size: 16px;
  color: white;
  padding: 20px 0px 25px 20px;
  width: 400px;
  height: 135px;
  overflow-y: scroll;
  
  &::-webkit-scrollbar {
    display: none;
  }
`
export const Warning = styled.div`
  color: #BABABA;
  font-size: 14px;
`

export const Accuracy = styled.div`
  color: white;
`