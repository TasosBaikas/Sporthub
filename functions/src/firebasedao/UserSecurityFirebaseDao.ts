/* eslint-disable */

import * as admin from "firebase-admin";
import {UserMatchAdminLimit} from "../models/user/usersecurity/UserMatchAdminLimit";

export class UserSecurityFirebaseDao {

    public getUserMatchAdminLimitTransaction(transaction: FirebaseFirestore.Transaction, userId:string) {

        const doc: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> = admin.firestore().collection("userMatchAdminLimit").doc(userId);

        return transaction.get(doc);
    }

    public updateUserMatchAdminLimitTransaction(transaction: FirebaseFirestore.Transaction, userMatchAdminLimit: UserMatchAdminLimit): FirebaseFirestore.Transaction {
        return this.saveUserMatchAdminLimitTransaction(transaction, userMatchAdminLimit);
    }

    saveUserMatchAdminLimitTransaction(transaction: FirebaseFirestore.Transaction, userMatchAdminLimit: UserMatchAdminLimit): FirebaseFirestore.Transaction {
        const doc: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> = admin.firestore().collection("userMatchAdminLimit").doc(userMatchAdminLimit.getUserId());

        return transaction.set(doc, userMatchAdminLimit.toObject());
    }

}
