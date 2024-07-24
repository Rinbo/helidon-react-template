import React, { useMemo, useState } from "react";
import { LoginState } from "./login-wrapper.tsx";
import { useFetcher } from "react-router-dom";
import Keypad from "./keypad.tsx";
import { FaUnlock } from "react-icons/fa";
import toast from "react-hot-toast";

type Props = {
  email: string;
  setState: React.Dispatch<React.SetStateAction<LoginState>>;
};

export default function PasscodeInsert({ email, setState }: Props) {
  const [reset, setReset] = useState<boolean>(false);
  const fetcher = useFetcher();

  const action = useMemo(() => fetcher.data, [fetcher.data]);
  const disabled = useMemo(() => fetcher.state !== "idle", [fetcher.state]);

  React.useEffect(() => {
    action?.error && onError(action.error);
  }, [action]);

  function onError(message: string) {
    toast.error(message);
    setReset(true);
  }

  async function submit(passcode: string) {
    fetcher.submit({ passcode, email }, { method: "post", action: "/authenticate", encType: "application/json" });
    reset && setReset(false);
  }

  return (
    <div className="flex flex-col items-center justify-center gap-4">
      <FaUnlock size={55} className="text-secondary" />
      <h1 className="text-lg uppercase">Passcode</h1>
      <h3 className="animate-bounce pt-2 text-center font-mono">Check your email for a passcode</h3>
      <div className={"flex flex-col gap-4"}>
        <Keypad disabled={disabled} submit={submit} reset={reset} />
        <button className="btn btn-ghost" onClick={() => setState("email-insert")} disabled={disabled}>
          Back
        </button>
      </div>
    </div>
  );
}
