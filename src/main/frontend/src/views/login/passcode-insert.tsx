import React, { useState } from "react";
import { LoginState } from "./login-wrapper.tsx";
import { authProvider } from "../../auth/auth.ts";
import { useNavigate, useRevalidator } from "react-router-dom";
import Keypad from "./keypad.tsx";
import { FaUnlock } from "react-icons/fa";

type Props = {
  email: string;
  setState: React.Dispatch<React.SetStateAction<LoginState>>;
};

export default function PasscodeInsert({ email, setState }: Props) {
  const [disabled, setDisabled] = useState<boolean>(false);
  const [reset, setReset] = useState<boolean>(false);
  const navigate = useNavigate();
  const revalidator = useRevalidator();

  async function submit(passcode: string) {
    setDisabled(true);
    try {
      await authProvider.authenticate({ email, passcode });
      revalidator.revalidate();
      navigate("/", { replace: true });
    } catch (error) {
      setReset(true);
      console.error(error, "Authentication failed");
    } finally {
      setDisabled(false);
    }
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
