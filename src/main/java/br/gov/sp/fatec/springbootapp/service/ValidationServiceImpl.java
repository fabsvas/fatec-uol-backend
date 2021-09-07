package br.gov.sp.fatec.springbootapp.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.gov.sp.fatec.springbootapp.entity.Profile;
import br.gov.sp.fatec.springbootapp.entity.Registration;
import br.gov.sp.fatec.springbootapp.repository.ProfileRepository;
import br.gov.sp.fatec.springbootapp.repository.RegistrationRepository;

@Service("ValidationService")
public class ValidationServiceImpl implements ValidationService {

    @Autowired
    private RegistrationRepository regRepo;

    @Autowired
    private ProfileRepository profRepo;

    @Transactional
    public Registration createRegistration(String email, String password, String name, String cellphone,
            String audioHash, String webGLHash, String canvasHash) {

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || cellphone.isEmpty() || audioHash.isEmpty()
                || webGLHash.isEmpty() || canvasHash.isEmpty()) {

            throw new RuntimeException("Invalid params");
        }

        Registration registration = regRepo.findByEmail(email);
        if (registration != null) {
            throw new RuntimeException("The email address must be unique");
        }

        registration = new Registration();
        registration.setEmail(email);
        registration.setPassword(password);
        registration.setName(name);
        registration.setCellphone(cellphone);
        regRepo.save(registration);

        Profile profile = profRepo.findByCanvasHashOrWebGLHashOrAudioHash(canvasHash, webGLHash, audioHash);
        if (profile == null) {
            profile = new Profile();
            profile.setUuid(UUID.randomUUID().toString());
            profile.setAudioHash(audioHash);
            profile.setCanvasHash(canvasHash);
            profile.setWebGLHash(webGLHash);
            profile.setRegistrations(new HashSet<Registration>());
        }

        profile.getRegistrations().add(registration);
        profRepo.save(profile);

        return registration;
    }

    public List<Profile> findAllProfiles() {
        return profRepo.findAll();
    }

    public Profile findProfileById(Long id) {
        Optional<Profile> profileOptional = profRepo.findById(id);
        if (profileOptional.isPresent()) {
            return profileOptional.get();
        }
        throw new RuntimeException("Profile not found for id: " + id);
    }

    public Profile findProfileByHash(String hash) {
        Profile profile = profRepo.findByCanvasHashOrWebGLHashOrAudioHash(hash, hash, hash);
        if (profile != null) {
            return profile;
        }
        throw new RuntimeException("Profile not found for hash: " + hash);
    }

    public List<Registration> findAllRegistrations() {
        return regRepo.findAll();
    }

    public Registration findRegistrationById(Long id) {
        Optional<Registration> registrationOptional = regRepo.findById(id);
        if (registrationOptional.isPresent()) {
            return registrationOptional.get();
        }
        throw new RuntimeException("Registration not found for id: " + id);
    }
}