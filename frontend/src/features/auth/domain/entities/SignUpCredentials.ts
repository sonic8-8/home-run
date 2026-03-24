export interface SignUpCredentials {
    readonly name: string;
    readonly email: string;
    readonly password: string;
    readonly passwordConfirm: string;
    readonly termsAgreed: boolean;
}
