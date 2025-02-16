/* eslint-disable */



import {Match} from "../../models/Match";

const functions = require("firebase-functions");
import {UserRepository} from "../../repository/UserRepository";
import {User} from "../../models/user/User";
import {UserFcmRepository} from "../../repository/UserFcmRepository";
import {messaging} from "firebase-admin";
import BatchResponse = messaging.BatchResponse;
import {UserShortForm} from "../../models/user/UserShortForm";


const userRepository: UserRepository = new UserRepository();
const userFcmRepository: UserFcmRepository = new UserFcmRepository();

export const newRequestNotifyAdmin = functions.region('europe-west1').firestore
  .document('matches/{sport}/matchesById/{matchById}')
  .onWrite(async (change: any, context: any) => {

      if (!change.before.exists)
          return null;

      console.log("1")
        const beforeMatch: Match = Match.makeInstanceFromSnapshot(change.before.data());
        const afterMatch: Match =  Match.makeInstanceFromSnapshot(change.after.data());

      console.log("2")

        const beforeRequests: string[] = beforeMatch.getUserRequestsToJoinMatch() || [];
        const afterRequests: string[] = afterMatch.getUserRequestsToJoinMatch() || [];

        if (beforeRequests.length + 1 === afterRequests.length) {
            const requesterId: string = afterRequests.filter((userIdTemp:string) => !beforeRequests.includes(userIdTemp))[0];
            console.log("3")

            const requester: User | null = await userRepository.getUserByIdPromise(requesterId, "", false);
            if (requester === null)
                return null;

            console.log("4")

            const requesterShortForm: UserShortForm = UserShortForm.makeShortForFromUser(requester,afterMatch.getSport());

            console.log("5")

            const adminId: string = afterMatch.getAdmin().getUserId();

            const fcmTokens:string[] = await userFcmRepository.getUserFcmTokens(adminId);
            if (fcmTokens.length === 0)
                return null;

            console.log("6")

            const payload = {
                data: {
                    pushNotificationType: "notifyAdminNewRequest",
                    match: JSON.stringify(afterMatch),
                    requester: JSON.stringify(requesterShortForm),
                },
                android: {
                    priority: 'high',
                },
                apns: {
                    headers: {
                        'apns-priority': '10',
                    },
                },
              };

            try {
                // Use sendEachForMulticast to send the notification to all tokens
                const response: BatchResponse = await userFcmRepository.sendEachForMulticast(fcmTokens,payload);

                console.log(`Sent ${response.successCount} messages successfully.`);
                if (response.failureCount > 0) {
                    console.error("Some messages failed:", response.responses);
                }
            } catch (error) {
                console.error("Error sending notification:", error);
            }
        }

        return null;

    })
