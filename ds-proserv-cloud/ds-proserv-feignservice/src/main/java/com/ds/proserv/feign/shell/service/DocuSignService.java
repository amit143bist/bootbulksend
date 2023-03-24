package com.ds.proserv.feign.shell.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ds.proserv.feign.shell.domain.Recipients;
import com.ds.proserv.feign.shell.domain.Signer;
import com.ds.proserv.feign.shell.domain.UpdateRecipientRequest;
import com.ds.proserv.feign.shell.domain.UpdateRecipientResponse;
import com.ds.proserv.feign.shell.domain.User;

public interface DocuSignService {

    @GetMapping(value = "/groups/{groupId}/users/{status}")
    public ResponseEntity<List<User>> getUsersByGroup(@PathVariable String groupId, @PathVariable String status);

    @GetMapping(value = "/envelope/{envelope}/recipients")
    public ResponseEntity<Recipients> getRecipients(@PathVariable("envelope") String envelope);

    @PutMapping(value = "/envelope/{envelope}/recipients")
    public ResponseEntity<UpdateRecipientResponse> updateRecipient(@RequestBody UpdateRecipientRequest request, @PathVariable("envelope") String envelope);

    @GetMapping(value = "/envelope/{envelope}/role/{roleName}")
    public ResponseEntity<Signer> getRecipientByRole(@PathVariable String roleName, @PathVariable("envelope") String envelope);
}
