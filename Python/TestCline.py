#!/usr/bin/env python
# -*- coding: utf-8 -*- 

import CriptBlock

recvblock = CriptBlock.ccCryptBlock()
sendblock = CriptBlock.ccCryptBlock()

def TestCline(cline):
    import socket, re, sys, array, time

    regExpr = re.compile('[CN]:\s*(\S+)+\s+(\d*)\s+(\S+)\s+([\w.-]+)')
    match = regExpr.search(cline)

    if match is None:
        return False;

    testSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    testSocket.settimeout(30)

    data = []
    host = match.group(1)
    port = int(match.group(2))
    username = match.group(3)
    password = match.group(4)

    try:
        ip = socket.gethostbyname(host)
        testSocket.connect((ip, port))
        DoHanshake(testSocket)

        try:
            userArray = GetPaddedUsername(username)
            rcount = Send(userArray, len(userArray), testSocket)
            
            passwordArray = GetPaddedPassword(password)
            sendblock.Encrypt(passwordArray, len(passwordArray))    
    
            cccamArray = GetCcam()
            rcount = Send(cccamArray, len(cccamArray), testSocket)

            testSocket.recv(20)
        except:
            print "Bad username/password for cline: " + cline
            return

        receivedBytes = bytearray(20)
        byteCount = 0
        for i in range(0, 5):
            byteCount = testSocket.recv_into(receivedBytes, 20)
            if byteCount > 0: break
            else: time.sleep(1)      
    
        if (byteCount > 0):
            print "SUCCESS! working cline: " + cline + " bytes: " + receivedBytes
        else:
            print "No ACK for cline: " + cline        
    except:
        print "Error in cline: " + cline

    testSocket.close()

def GetPaddedUsername(userName):
    import array

    #We create an array of 20 bytes with the username in it as bytes and padded with 0 behind
    #Like: [23,33,64,13,0,0,0,0,0,0,0...]
    userBytes = array.array("B", userName)
    userByteArray = FillArray(bytearray(20), userBytes)

    return userByteArray

def GetCcam():
    import array

    #We create an array of 5 bytes with the "CCcam" in it as bytes
    cccamBytes = array.array("B", "CCcam")    
    cccamByteArray = FillArray(bytearray(5), cccamBytes)
    return cccamByteArray

def GetPaddedPassword(password):
    import array

    #We create an array of 63 bytes with the password in it as bytes and padded with 0 behind
    #Like: [23,33,64,13,0,0,0,0,0,0,0...]
    passwordBytes = array.array("B", password)
    passwordByteArray = FillArray(bytearray(63),passwordBytes)

    return passwordByteArray
    
def DoHanshake(socket):
    import hashlib, array, CriptBlock

    random = bytearray(16)
    socket.recv_into(random, 16) #Receive first 16 "Hello" random bytes
    print "Hello bytes: " + random

    random = CriptBlock.Xor(random); #Do a Xor with "CCcam" string to the hello bytes

    sha1 = hashlib.sha1()
    sha1.update(random)
    sha1digest = array.array('B', sha1.digest()) #Create a sha1 hash with the xor hello bytes
    sha1hash = FillArray(bytearray(20), sha1digest)

    recvblock.Init(sha1hash, 20) #initialize the receive handler
    recvblock.Decrypt(random, 16)

    sendblock.Init(random, 16) #initialize the send handler
    sendblock.Decrypt(sha1hash, 20)

    rcount = Send(sha1hash, 20, socket) #Send the a crypted sha1hash!    
    
def Send(data, len, socket):
    buffer = FillArray(bytearray(len), data)
    sendblock.Encrypt(buffer, len)
    rcount = socket.send(buffer)
    return rcount

def FillArray(array, source):
    if len(source) <= len(array):
        for i in range(0, len(source)):
            array[i] = source[i]
    else:
        for i in range(0, len(array)):
            array[i] = source[i]
    return array
