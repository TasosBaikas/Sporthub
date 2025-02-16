/* eslint-disable */



import {ChatMessage} from "../../models/chat/ChatMessage";
import {Comparator} from "../../interfaces/Comparator";

export class ChatMessageComparator implements Comparator<ChatMessage>{
    constructor() {
    }

    public compare(chatMessage1:ChatMessage, chatMessage2:ChatMessage):number {
        if (chatMessage1 == null && chatMessage2 == null) return 0;
        if (chatMessage1 == null) return 1;
        if (chatMessage2 == null) return -1;

        const createdAtUTC1: number = chatMessage1.getCreatedAtUTC();
        const createdAtUTC2: number = chatMessage2.getCreatedAtUTC();

        if (createdAtUTC1 < createdAtUTC2) return 1;
        if (createdAtUTC1 > createdAtUTC2) return -1;
        return 0;
    }
}
