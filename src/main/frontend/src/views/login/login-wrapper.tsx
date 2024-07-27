import React, { useState } from "react";
import EmailInsert from "./email-insert.tsx";
import PasscodeInsert from "./passcode-insert.tsx";

export type LoginState = "email-insert" | "passcode-insert";

export default function LoginWrapper({ passedEmail }: { passedEmail?: string }) {
  const [state, setState] = useState<LoginState>("email-insert");
  const [email, setEmail] = useState<string>(passedEmail ?? "");

  React.useEffect(() => {
    if (email !== "") setState("passcode-insert");
  }, [email]);

  const renderState = () => {
    switch (state) {
      case "email-insert":
        return <EmailInsert setEmail={setEmail} />;

      case "passcode-insert":
        return <PasscodeInsert email={email} setState={setState} />;
    }
  };

  return <div className="w-full max-w-sm">{renderState()}</div>;
}
