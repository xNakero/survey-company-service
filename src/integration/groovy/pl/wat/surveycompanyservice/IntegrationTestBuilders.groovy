package pl.wat.surveycompanyservice

class IntegrationTestBuilders {

    static String RESEARCHER_USERNAME = "researcher@gmail.com"
    static String PARTICIPANT_USERNAME = "participant@gmail.com"
    static String PASSWORD = "password"
    static String JWT_REGEX = '^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$'

    static Map researcherRegistrationRequest(Map params = [:]) {
        return [
                username    : params.username ?: RESEARCHER_USERNAME,
                password    : params.password ?: PASSWORD,
                role        : "RESEARCHER"
        ]
    }

    static Map participantRegistrationRequest(Map params = [:]) {
        return [
                username    : params.username ?: PARTICIPANT_USERNAME,
                password    : params.password ?: PASSWORD,
                role        : "PARTICIPANT"
        ]
    }

    static Map loginRequest(Map params = [:]) {
        return [
                username    : params.username ?: RESEARCHER_USERNAME,
                password    : params.password ?: PASSWORD
        ]
    }
}
