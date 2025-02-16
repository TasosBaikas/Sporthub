/* eslint-disable */



import {User} from "../../models/user/User";
import {MatchRepository} from "../../repository/MatchRepository";
import {Match} from "../../models/Match";
import {UserShortForm} from "../../models/user/UserShortForm";
import {ChatRepository} from "../../repository/ChatRepository";
import {Chat} from "../../models/chat/Chat";
import {ChatMessage} from "../../models/chat/ChatMessage";

const functions = require("firebase-functions");

const matchRepository:MatchRepository = new MatchRepository();
const chatRepository:ChatRepository = new ChatRepository();

export const userDataChangeTrigger = functions.region('europe-west1').firestore
  .document('users/{userId}')
  .onWrite(async (change: any, context: any) => {

      if (!change.before.exists || !change.after.exists)
          return null;

        const userBefore: User = User.makeInstanceFromSnapshot(change.before.data());
        const userAfter: User = User.makeInstanceFromSnapshot(change.after.data());

        console.log("before userBefore.equals");
        if (userBefore.equals(userAfter)){
            console.log("objects are equal");
            return null
        }

        const matchesOfUserThatHisAdmin: Match[] = await matchRepository.getMatchesByAdminIdAllSportsPromise(userAfter.getUserId());

        const matchSavePromises: Promise<null>[] = []
        matchesOfUserThatHisAdmin.forEach((match:Match) => {

            match.setAdmin(UserShortForm.makeShortForFromUser(userAfter,match.getSport()));
            match.setLatitude(userAfter.getLatitude());
            match.setLongitude(userAfter.getLongitude());

            matchSavePromises.push(matchRepository.updateMatchIfAdminChangeSomeValues(match, userAfter.getUserId()));
        });

        const chatThatYouHaveSendLastMessage:Chat[] = await chatRepository.getChatsByUserThatSendLastMessage(userAfter.getUserId());

        const chatsLastMessageSavePromises:Promise<null>[] = [];
        chatThatYouHaveSendLastMessage.forEach((chat:Chat) => {


            const lastChatMessage: ChatMessage | null = chat.getLastChatMessage();
            if (lastChatMessage == null)
                return;

            if (chat.isPrivateConversation()){

                chatsLastMessageSavePromises.push(chatRepository.updateChatLastMessageUserThatSendMessage(chat,UserShortForm.makeShortForFromUser(userAfter,null)));
            }else{

                chatsLastMessageSavePromises.push(chatRepository.updateChatLastMessageUserThatSendMessage(chat,UserShortForm.makeShortForFromUser(userAfter,chat.getSport())));
            }

        });

        const privateChats:Chat[] = await chatRepository.getPrivateChatsByUserId(userAfter.getUserId());

      const chatPrivatesSave:Promise<null>[] = [];
      privateChats.forEach((privateChat:Chat) => {

          const membersOfPrivate: UserShortForm[] = privateChat.getPrivateConversation2Users()
              .filter((user: UserShortForm) => user.getUserId() !== userAfter.getUserId())

          membersOfPrivate.push(UserShortForm.makeShortForFromUser(userAfter,null));

          privateChat.setPrivateConversation2Users(membersOfPrivate);

          chatPrivatesSave.push(chatRepository.updateChatPrivate(privateChat));
      });


        await Promise.allSettled(matchSavePromises);
        await Promise.allSettled(chatsLastMessageSavePromises);
        await Promise.allSettled(chatPrivatesSave);

        return null;

    })
