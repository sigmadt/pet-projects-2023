// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: gen/Torrent.proto

package ru.itmo.java.message.tracker;

public interface TrackerResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ru.itmo.java.message.tracker.TrackerResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.ru.itmo.java.message.tracker.ListResponse listResponse = 1;</code>
   * @return Whether the listResponse field is set.
   */
  boolean hasListResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.ListResponse listResponse = 1;</code>
   * @return The listResponse.
   */
  ru.itmo.java.message.tracker.ListResponse getListResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.ListResponse listResponse = 1;</code>
   */
  ru.itmo.java.message.tracker.ListResponseOrBuilder getListResponseOrBuilder();

  /**
   * <code>.ru.itmo.java.message.tracker.UploadResponse uploadResponse = 2;</code>
   * @return Whether the uploadResponse field is set.
   */
  boolean hasUploadResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.UploadResponse uploadResponse = 2;</code>
   * @return The uploadResponse.
   */
  ru.itmo.java.message.tracker.UploadResponse getUploadResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.UploadResponse uploadResponse = 2;</code>
   */
  ru.itmo.java.message.tracker.UploadResponseOrBuilder getUploadResponseOrBuilder();

  /**
   * <code>.ru.itmo.java.message.tracker.SourceResponse sourceResponse = 3;</code>
   * @return Whether the sourceResponse field is set.
   */
  boolean hasSourceResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.SourceResponse sourceResponse = 3;</code>
   * @return The sourceResponse.
   */
  ru.itmo.java.message.tracker.SourceResponse getSourceResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.SourceResponse sourceResponse = 3;</code>
   */
  ru.itmo.java.message.tracker.SourceResponseOrBuilder getSourceResponseOrBuilder();

  /**
   * <code>.ru.itmo.java.message.tracker.UpdateResponse updateResponse = 4;</code>
   * @return Whether the updateResponse field is set.
   */
  boolean hasUpdateResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.UpdateResponse updateResponse = 4;</code>
   * @return The updateResponse.
   */
  ru.itmo.java.message.tracker.UpdateResponse getUpdateResponse();
  /**
   * <code>.ru.itmo.java.message.tracker.UpdateResponse updateResponse = 4;</code>
   */
  ru.itmo.java.message.tracker.UpdateResponseOrBuilder getUpdateResponseOrBuilder();

  public ru.itmo.java.message.tracker.TrackerResponse.ResponseCase getResponseCase();
}
